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

import java.util.Objects;

public class Parent {
	final int id;
	final String sfId;
	final Member member;

	Integer requiredField1;
	String requiredField2;
	String requiredField3;
	String sfId1;
	String sfId2;

	public Parent(
			int id,
			String sfId,
			Member member,
			Integer requiredField1,
			String requiredField2,
			String requiredField3,
			String sfId1,
			String sfId2) {
		this.id = id;
		this.sfId = sfId;
		this.member = member;
		this.requiredField1 = requiredField1;
		this.requiredField2 = requiredField2;
		this.requiredField3 = requiredField3;
		this.sfId1 = sfId1;
		this.sfId2 = sfId2;
	}

	public Parent(int id, String sfId, Member member) {
		this(id, sfId, member, null, null, null, null, null);
	}

	public int getId() {
		return id;
	}

	public String getSfId() {
		return sfId;
	}

	public Member getMember() {
		return member;
	}

	public Integer getRequiredField1() {
		return requiredField1;
	}

	public String getRequiredField2() {
		return requiredField2;
	}

	public String getRequiredField3() {
		return requiredField3;
	}

	public String getSfId1() {
		return sfId1;
	}

	public String getSfId2() {
		return sfId2;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Parent parent) || !parent.canEqual(this)) {
			return false;
		}
		return getId() == parent.getId()
				&& Objects.equals(getSfId(), parent.getSfId())
				&& Objects.equals(getMember(), parent.getMember())
				&& Objects.equals(getRequiredField1(), parent.getRequiredField1())
				&& Objects.equals(getRequiredField2(), parent.getRequiredField2())
				&& Objects.equals(getRequiredField3(), parent.getRequiredField3())
				&& Objects.equals(getSfId1(), parent.getSfId1())
				&& Objects.equals(getSfId2(), parent.getSfId2());
	}

	protected boolean canEqual(Object other) {
		return other instanceof Parent;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				getId(),
				getSfId(),
				getMember(),
				getRequiredField1(),
				getRequiredField2(),
				getRequiredField3(),
				getSfId1(),
				getSfId2());
	}

	@Override
	public String toString() {
		return "Parent(id="
				+ id
				+ ", sfId="
				+ sfId
				+ ", member="
				+ member
				+ ", requiredField1="
				+ requiredField1
				+ ", requiredField2="
				+ requiredField2
				+ ", requiredField3="
				+ requiredField3
				+ ", sfId1="
				+ sfId1
				+ ", sfId2="
				+ sfId2
				+ ")";
	}
}
