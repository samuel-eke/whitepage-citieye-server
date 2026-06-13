package com.goldbyte.citieye_server.admin_content_cms.internal.service;

import com.goldbyte.citieye_server.admin_content_cms.internal.entity.AdminCMSContentEntity;
import com.goldbyte.citieye_server.admin_content_cms.internal.repo.AdminCMSContentEntityRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceAdminCMS {
    private final AdminCMSContentEntityRepository adminCMSContentEntityRepository;


    ServiceAdminCMS(AdminCMSContentEntityRepository adminCMSContentEntityRepository) {
        this.adminCMSContentEntityRepository = adminCMSContentEntityRepository;
    }

    @Transactional
    public AdminCMSContentEntity createAPost(AdminCMSContentEntity cmsContent){
        return adminCMSContentEntityRepository.save(cmsContent);
    }

    public List<AdminCMSContentEntity> viewAllPosts(){
        return adminCMSContentEntityRepository.findAll();
    }
}
