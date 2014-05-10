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

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.quartzsource.meutrino.*;
import scala.Option;
import scala.Some;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class BasicTaskTest {

	@Test
	public void testCreate() throws IOException {
		MyFactory factory = new MyFactory();
		File path = new File(factory.getTempFolder(), "R-"
				+ System.currentTimeMillis());
		path.mkdir();
		System.out.println("Repo: " + path);
		assertFalse(new File(path, ".hg").exists());
		JavaQRepository repo = factory.create(path);
		assertTrue(new File(path, ".hg").exists());
		List<Tuple2<QStatus, QPath>> added = repo.getStatus();
		assertEquals(0, added.size());
		// copy a file
		File resource1 = new File("./src/test/resources/file1.txt");
		assertTrue(resource1.exists());
		FileUtils.copyFile(resource1, new File(path, "file1.txt"));
		repo.addRemove(new ArrayList<QPath>(), 100);
		added = repo.getStatus();
		assertEquals(1, added.size());
		assertEquals("file1.txt", added.get(0)._2.getPath());
		assertEquals(ADDED$.MODULE$, added.get(0)._1);
		// commit
		Option<String> none = Option.apply(null);
		Option<Date> now = Option.apply(new Date());
		Tuple2<Object, QNodeId> node = repo.commit("desription1", none, none,
				false, false, now);
		assertEquals(0, repo.getStatus().size());
		assertTrue(node._2.getNode().matches("[0-9a-f]{40}"));
		repo.close();
		// re-open
		JavaQRepository repo2 = factory.open(path);
		String content = repo2.cat(new QPath("file1.txt"), new Some<QNodeId>(
				node._2));
		assertEquals("line1\n", content);
		repo2.close();
		// clone
		File clonePath = new File(factory.getTempFolder(), "Clone-"
				+ System.currentTimeMillis());
		JavaQRepository repo3 = factory.clone(path.getCanonicalPath(),
				clonePath);
		assertEquals("line1\n",
				repo3.cat(new QPath("file1.txt"), new Some<QNodeId>(node._2)));
		repo3.close();
	}
}
