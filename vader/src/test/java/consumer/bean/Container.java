/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.bean;

import com.force.swag.id.ID;
import lombok.ToString;

/**
 * Connect API input bean.
 *
 * @author gopala.akshintala
 * @since 232
 */
@ToString(callSuper = true)
public class Container extends Parent {

  public Container(int id, Member member) {
    super(id, null, member);
  }

  public Container(int id) {
    super(id, null, null);
  }

  public Container(ID sfId1) {
    super(0, sfId1, null);
  }
}
