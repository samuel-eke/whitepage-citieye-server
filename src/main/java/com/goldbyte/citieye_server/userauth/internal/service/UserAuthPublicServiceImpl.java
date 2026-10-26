package com.goldbyte.citieye_server.userauth.internal.service;

import com.goldbyte.citieye_server.userauth.UserAuthPublicService;
import com.goldbyte.citieye_server.userauth.internal.model.AdminUser;
import com.goldbyte.citieye_server.userauth.internal.repo.AdminUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserAuthPublicServiceImpl implements UserAuthPublicService {

    private final AdminUserRepository adminUserRepository;

    public UserAuthPublicServiceImpl(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getOfficerStateOfResidence(String officerCode) {
        if (officerCode == null || officerCode.isBlank()) {
            return Optional.empty();
        }
        return adminUserRepository.findFirstByCitizenCode(officerCode.trim())
                .filter(user -> user.getStateOfResidence() != null)
                .map(user -> user.getStateOfResidence().name().replace('_', ' '));
    }
}
