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

import org.junit.Test;
import org.quartzsource.meutrino.*;
import org.quartzsource.meutrino.client.CommandServerConfig;
import org.quartzsource.meutrino.client.CommandServerFactory;
import scala.Option;
import scala.Tuple2;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class CommandServerFactoryTest {

	@Test
	public void testConverterListToJava() {
		CommandServerFactory factory = new CommandServerFactory("hg",
				CommandServerConfig.apply(null,
						new HashMap<String, java.util.Map<String, String>>(),
						false, new HashMap<String, String>(), false));
		QRepository repo = factory.open(new File("."));
		Option<QNodeId> none = Option.apply(null);
		Object some = repo.status(none, false, false, false);
		System.out.println("Scala collection: " + some);
		List<Tuple2<QStatus, QPath>> resources = Converter.listToJava(repo
				.status(none, false, false, false));
		for (Tuple2<QStatus, QPath> tuple : resources) {
			System.out.println(tuple);
		}
		repo.close();
		System.out.println("Closed.");
	}
}
