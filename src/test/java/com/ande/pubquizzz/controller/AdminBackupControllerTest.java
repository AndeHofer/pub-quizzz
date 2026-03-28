package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.exception.BusinessValidationException;
import com.ande.pubquizzz.exception.GlobalExceptionHandler;
import com.ande.pubquizzz.security.SecurityConfig;
import com.ande.pubquizzz.service.BackupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBackupController.class)
@Import({SecurityConfig.class, SecurityAutoConfiguration.class, ServletWebSecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, SecurityTestConfig.class, GlobalExceptionHandler.class})
class AdminBackupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BackupService backupService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void export_asAdmin_returnsZipContentType() throws Exception {
        doNothing().when(backupService).createBackup(any());

        mockMvc.perform(get("/admin/backup/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/zip")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void export_asAdmin_hasFilenameHeader() throws Exception {
        doNothing().when(backupService).createBackup(any());

        mockMvc.perform(get("/admin/backup/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(".zip")));
    }

    @Test
    void export_unauthenticated_returns302() throws Exception {
        mockMvc.perform(get("/admin/backup/export"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void import_withValidZip_returns200WithGermanMessage() throws Exception {
        doNothing().when(backupService).stageRestore(any(byte[].class));

        MockMultipartFile zipFile = new MockMultipartFile(
                "file", "backup.zip", "application/zip", "PK fake zip".getBytes());

        mockMvc.perform(multipart("/admin/backup/import").file(zipFile))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Backup bereit")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void import_withInvalidZip_returns400() throws Exception {
        doThrow(new BusinessValidationException("Ungültige Backup-Datei"))
                .when(backupService).stageRestore(any(byte[].class));

        MockMultipartFile zipFile = new MockMultipartFile(
                "file", "backup.zip", "application/zip", "not a real zip".getBytes());

        mockMvc.perform(multipart("/admin/backup/import").file(zipFile))
                .andExpect(status().isBadRequest());
    }
}
