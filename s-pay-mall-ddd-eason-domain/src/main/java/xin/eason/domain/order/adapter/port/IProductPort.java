package xin.eason.domain.order.adapter.port;

import xin.eason.domain.order.model.entity.ProductEntity;

public interface IProductPort {

    /**
     * 根据产品 ID 查询产品信息，返回产品实体对象
     * @param productId 产品 ID
     * @return 产品实体对象
     */
    ProductEntity queryProductByProductId(String productId);
}
