package cn.edu.sdu.sms.server.service;

import cn.edu.sdu.sms.server.models.AttachmentItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AttachmentService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<AttachmentItem> parseAttachments(String json) {
        if (json == null || json.isEmpty() || "[]".equals(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<AttachmentItem>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String addAttachment(String currentJson, AttachmentItem newItem) {
        List<AttachmentItem> list = parseAttachments(currentJson);
        list.add(newItem);
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize attachments");
        }
    }

    public String removeAttachment(String currentJson, int index) {
        List<AttachmentItem> list = parseAttachments(currentJson);
        list.remove(index);
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize attachments");
        }
    }
}
