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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.quartzsource.meutrino.ADDED$;
import org.quartzsource.meutrino.Converter;
import org.quartzsource.meutrino.JavaQRepository;
import org.quartzsource.meutrino.QChangeContext;
import org.quartzsource.meutrino.QNodeId;
import org.quartzsource.meutrino.QPath;
import org.quartzsource.meutrino.QStatus;

import scala.Option;
import scala.Tuple2;

public class QChangeContextTest {

	@Test
	public void testCreate() throws IOException {
		MyFactory factory = new MyFactory();
		File path = new File(factory.getTempFolder(), "R-"
				+ System.currentTimeMillis());
		path.mkdir();
		JavaQRepository repo = factory.create(path);
		List<Tuple2<QStatus, QPath>> added = repo.getStatus();
		// copy a file
		File resource1 = new File("./src/test/resources/file1.txt");
		assertTrue(resource1.exists());
		FileUtils.copyFile(resource1, new File(path, "file1.txt"));
		repo.addRemove(new ArrayList<QPath>(), 100);
		added = repo.getStatus();
		assertEquals(ADDED$.MODULE$, added.get(0)._1);
		// commit
		Option<String> none = Option.apply(null);

		Tuple2<Object, QNodeId> node = repo.commit("desription1", none, none,
				false, false, new Date());
		QChangeContext context = repo.apply(node._2);
		List<QPath> files = Converter.listToJava(context.toList());
		assertEquals(1, files.size());
		assertEquals("file1.txt", files.get(0).getPath());
	}
}
