/*******************************************************************************
 * Copyright (c) 2014 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.compare.git.pgm.util;

import static org.eclipse.emf.compare.git.pgm.internal.util.EMFCompareGitPGMUtil.EOL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:arthur.daussy@obeo.fr">Arthur Daussy</a>
 */
@SuppressWarnings("nls")
public class ProjectBuilder {

	private final Object startingPointForRelativePath;

	private List<String> relativePathContentToCopy = new ArrayList<>();

	private Map<String, String> newFilesContent = new HashMap<>();

	private boolean clean = false;

	public ProjectBuilder(Object startingPointForRelativePath) {
		super();
		this.startingPointForRelativePath = startingPointForRelativePath;
	}

	public ProjectBuilder addContentToCopy(String relativePathContent) {
		relativePathContentToCopy.add(relativePathContent);
		return this;
	}

	public ProjectBuilder addNewFileContent(String relativePath, String content) {
		newFilesContent.put(relativePath, content);
		return this;
	}

	public ProjectBuilder clean(boolean needClean) {
		this.clean = needClean;
		return this;
	}

	private void cleanBeforeCreate(Path to) {
		File file = to.toFile();
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					cleanBeforeCreate(f.toPath());
				}
			}
			file.delete();
		}

	}

	private String getFileName(String relativePath) {
		return FileSystems.getDefault().getPath(relativePath).getFileName().toString();
	}

	public File create(Path to) throws IOException {
		Preconditions.checkNotNull(to);
		if (clean) {
			cleanBeforeCreate(to);
		}
		File project = createProject(to);

		// Creates new file with content
		for (Entry<String, String> newFileEntry : newFilesContent.entrySet()) {
			createFile(project.toPath().resolve(newFileEntry.getKey()), newFileEntry.getValue());
		}

		// Creates new content from copy
		for (String relativePath : relativePathContentToCopy) {
			InputStream stream = startingPointForRelativePath.getClass().getResourceAsStream(relativePath);
			assertNotNull(stream);
			try {
				Files.copy(stream, to.resolve(getFileName(relativePath)), StandardCopyOption.REPLACE_EXISTING);
			} finally {
				stream.close();
			}
		}

		return project;
	}

	protected File createProject(Path projectPath) throws IOException {
		File project = projectPath.toFile();
		if (!project.mkdirs()) {
			throw new AssertionError("Can create a project at " + projectPath.toString());
		}
		// Creates ".project" file
		File projectFile = projectPath.resolve(".project").toFile();
		if (projectFile.exists()) {
			projectFile.delete();
		}
		projectFile.createNewFile();
		PrintWriter writer = new PrintWriter(projectFile);
		StringBuilder content = new StringBuilder();
		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(EOL);
		content.append("<projectDescription>").append(EOL);
		content.append("<name>").append(projectPath.getFileName()).append("</name>").append(EOL);
		content.append("<comment></comment>").append(EOL);
		content.append("<projects></projects>").append(EOL);
		content.append("<buildSpec></buildSpec>").append(EOL);
		content.append("<natures></natures>").append(EOL);
		content.append("</projectDescription>").append(EOL);
		writer.print(content.toString());
		writer.close();
		assertFalse(writer.checkError());
		return project;
	}

	protected File createFile(Path path, String content) throws FileNotFoundException,
			UnsupportedEncodingException {
		File result = path.toFile();
		path.getParent().toFile().mkdirs();
		PrintWriter writer = new PrintWriter(result, "UTF-8"); //$NON-NLS-1$
		writer.println(content);
		writer.close();
		assertFalse(writer.checkError());
		return result;
	}

}
