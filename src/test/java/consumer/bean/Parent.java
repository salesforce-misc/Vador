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
 * @author gopala.akshintala
 * @since 232
 */
public class Parent extends BaseParent {

    public Parent(int id, Child child) {
        super(id, child);
    }
    
    public Parent(int id) {
        super(id, null);
    }
}
