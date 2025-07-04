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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public class Parent {
	final int id;
	final String sfId;
	final Member member;

	Integer requiredField1;
	String requiredField2;
	String requiredField3;
	String sfId1;
	String sfId2;
}
