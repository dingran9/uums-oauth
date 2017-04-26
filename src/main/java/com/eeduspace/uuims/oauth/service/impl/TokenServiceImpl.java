package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.persist.dao.TokenDao;
import com.eeduspace.uuims.oauth.persist.enumeration.SourceEnum;
import com.eeduspace.uuims.oauth.persist.po.TokenPo;
import com.eeduspace.uuims.oauth.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:令牌管理
 */
@Service
public class TokenServiceImpl implements TokenService {

    private final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Inject
    private TokenDao tokenDao;

    @Override
    public List<TokenPo> findAll() {
        return (List<TokenPo>) tokenDao.findAll();
    }

    @Override
    public List<TokenPo> findByOpenIdAndPIdAndEquipmentType(String openId,Long productId,SourceEnum.EquipmentType type) {
        //todo 亿家教 终端类型进行合并
        if(type.equals(SourceEnum.EquipmentType.Android) || type.equals(SourceEnum.EquipmentType.Ios)){
            return tokenDao.findByOpenIdAndProductIdAndEquipmentType(openId, productId);
        }
        return tokenDao.findByOpenIdAndProductIdAndEquipmentType(openId, productId, type);
    }

    @Override
    public TokenPo findOne(Long tokenId) {
        return tokenDao.findOne(tokenId);
    }

    @Override
    public TokenPo findByRefreshToken(String refreshToken) {
        return tokenDao.findByRefreshToken(refreshToken);
    }

    @Override
    public TokenPo save(TokenPo TokenPo) {
        return tokenDao.save(TokenPo);
    }

    @Override
    @Transactional
    public TokenPo save(TokenPo TokenPo, TokenPo deletePo) {
        tokenDao.delete(deletePo.getId());
        return tokenDao.save(TokenPo);
    }

    @Override
    @Transactional
    public TokenPo save(TokenPo TokenPo, String openId) {
       // tokenDao.deleteByOpenId(openId);
        return tokenDao.save(TokenPo);
    }

    @Override
    public void delete(Long id) {
        tokenDao.delete(id);
    }

    @Override
    public void delete(TokenPo tokenPo) {
        tokenDao.delete(tokenPo);
    }
}
