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

// import com.force.swag.id.ID;

public class Container extends Parent {

	public Container(int id, Member member) {
		super(id, null, member);
	}

	public Container(int id) {
		super(id, null, null);
	}

	public Container(String sfId1) {
		super(0, sfId1, null);
	}

	@Override
	public String toString() {
		return "Container(super=" + super.toString() + ")";
	}
}
