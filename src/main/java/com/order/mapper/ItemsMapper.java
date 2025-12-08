package com.order.mapper;

import com.order.bean.Items;
import com.order.dto.ItemDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemsMapper {
    List<Items> toEntity(List<ItemDTO> request);

    ItemDTO toResource(Items items);
}
