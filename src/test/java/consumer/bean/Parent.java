/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.bean;

import com.force.swag.id.ID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Connect API input bean.
 *
 * @author gakshintala
 * @since 228
 */
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public class Parent {
  final int id;
  final ID sfId;
  final Member member;

  Integer requiredField1;
  String requiredField2;
  String requiredField3;
  ID sfId1;
  ID sfId2;
}
