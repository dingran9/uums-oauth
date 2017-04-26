package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.persist.dao.CodeDao;
import com.eeduspace.uuims.oauth.persist.po.CodePo;
import com.eeduspace.uuims.oauth.service.CodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/10/26
 * Description:CODE码管理
 */
@Service
public class CodeServiceImpl implements CodeService {

    private final Logger logger = LoggerFactory.getLogger(CodeServiceImpl.class);

    @Inject
    private CodeDao codeDao;

    @Override
    public List<CodePo> findAll() {
        return (List<CodePo>) codeDao.findAll();
    }

    @Override
    public CodePo findOne(Long codeId) {
        return codeDao.findOne(codeId);
    }

    @Override
    public CodePo save(CodePo CodePo) {
        return codeDao.save(CodePo);
    }

    @Override
    public void delete(Long id) {
        codeDao.delete(id);
    }
}
