/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.bean;

import java.util.Objects;

public final class Member {
	private final int id;

	public Member(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object other) {
		return this == other || other instanceof Member member && getId() == member.getId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	@Override
	public String toString() {
		return "Member(id=" + id + ")";
	}
}
