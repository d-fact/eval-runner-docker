package cslicer.daikon;

/*
 * #%L
 * CSlicer
 *    ______ _____  __ _                  
 *   / ____// ___/ / /(_)_____ ___   _____
 *  / /     \__ \ / // // ___// _ \ / ___/
 * / /___  ___/ // // // /__ /  __// /
 * \____/ /____//_//_/ \___/ \___//_/
 * %%
 * Copyright (C) 2014 - 2021 Department of Computer Science, University of Toronto
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class InvariantsRank {
	public static final int BASE_RANK = 0;

	public static Map<Invariant, Integer> invariatRankMap = new TreeMap<>();

	public static void increaseInvariantsRank(Set<Invariant> SetAminusSetB) {
		for (Invariant inv : SetAminusSetB) {
			if (invariatRankMap.get(inv) == null) {
				System.out.println("[ERROR]: invariant is not in the map!");
				return;
			} else {
				int newRank = invariatRankMap.get(inv) + 1;
				invariatRankMap.put(inv, newRank);
			}
		}
	}

	public static void decreaseInvariantsRank(Set<Invariant> SetAminusSetB) {
		for (Invariant inv : SetAminusSetB) {
			if (invariatRankMap.get(inv) == null) {
				System.out.println("[ERROR]: invariant is not in the map!");
				return;
			} else {
				int newRank = invariatRankMap.get(inv) - 1;
				invariatRankMap.put(inv, newRank);
			}
		}
	}
}
