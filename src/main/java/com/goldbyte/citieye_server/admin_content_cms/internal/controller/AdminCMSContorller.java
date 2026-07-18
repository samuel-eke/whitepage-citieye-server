package com.goldbyte.citieye_server.admin_content_cms.internal.controller;

import com.goldbyte.citieye_server.admin_content_cms.internal.entity.AdminCMSContentEntity;
import com.goldbyte.citieye_server.admin_content_cms.internal.service.ServiceAdminCMS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/content")
class AdminCMSContorller {
    private final ServiceAdminCMS serviceAdminCMS;

    AdminCMSContorller(ServiceAdminCMS serviceAdminCMS) {
        this.serviceAdminCMS = serviceAdminCMS;
    }

    @PostMapping("/new")
    public ResponseEntity<AdminCMSContentEntity> createContent(@RequestBody AdminCMSContentEntity cmsContent) {
        AdminCMSContentEntity newPost = serviceAdminCMS.createAPost(cmsContent);
        return ResponseEntity.ok(newPost);
    }

    @GetMapping
    public ResponseEntity<List<AdminCMSContentEntity>> getAllPosts() {
        List<AdminCMSContentEntity> allPosts = serviceAdminCMS.viewAllPosts();
        return ResponseEntity.ok(allPosts);
    }

}
