package com.goldbyte.citieye_server.admin_content_cms.internal.repo;

import com.goldbyte.citieye_server.admin_content_cms.internal.entity.AdminCMSContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminCMSContentEntityRepository extends JpaRepository<AdminCMSContentEntity, UUID> {
}