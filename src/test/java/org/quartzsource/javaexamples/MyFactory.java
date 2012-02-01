/**
 * Copyright (c) 2012, www.quartzsource.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quartzsource.javaexamples;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.quartzsource.meutrino.JavaQRepository;
import org.quartzsource.meutrino.client.CommandServerFactory;

public class MyFactory {
	private CommandServerFactory worker;
	private File tempFolder;

	public MyFactory() {
		Map<String, Map<String, String>> config = new HashMap<String, java.util.Map<String, String>>();
		Map<String, String> uiMap = new HashMap<String, String>();
		uiMap.put("username", "py4fun");
		config.put("ui", uiMap);
		this.worker = new CommandServerFactory("hg", null, config, false,
				new HashMap<String, String>());
		File root = new File(System.getProperty("java.io.tmpdir"));
		tempFolder = new File(root, "javaexample_test");
		tempFolder.mkdirs();
	}

	public JavaQRepository create(File path) {
		JavaQRepository repo = (JavaQRepository) worker.create(path);
		return repo;
	}

	public JavaQRepository open(File path) {
		JavaQRepository repo = (JavaQRepository) worker.open(path);
		return repo;
	}

	public JavaQRepository clone(String source, File path) {
		JavaQRepository repo = (JavaQRepository) worker.clone(source, path,
				false, false);
		return repo;
	}

	/**
	 * to support testing
	 */
	public File getTempFolder() {
		return tempFolder;
	}
}
