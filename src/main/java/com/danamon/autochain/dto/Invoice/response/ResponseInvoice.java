package com.danamon.autochain.dto.Invoice.response;

import com.danamon.autochain.entity.ItemList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.*;
import org.json.JSONArray;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseInvoice {
    private String companyName;
    private String invNumber;
    private Long amount;
    private Date dueDate;
    private String Status;
    private String type;
    private List<ItemList> itemList;
}
