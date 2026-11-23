package com.smartattend.controller;

import com.smartattend.model.dto.ApiResponse;
import com.smartattend.model.dto.StudentDto;
import com.smartattend.model.entity.FaceReference;
import com.smartattend.model.entity.User;
import com.smartattend.service.AuthService;
import com.smartattend.service.FaceReferenceService;
import com.smartattend.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FaceReferenceController {

    private final FaceReferenceService faceReferenceService;
    private final AuthService authService;
    private final StudentService studentService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerFace(
            @RequestParam(required = false) Long studentId,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestBody(required = false) Map<String, String> body) throws IOException {

        Long targetStudentId = studentId;
        if (targetStudentId == null) {
            User currentUser = authService.getCurrentUser();
            StudentDto studentDto = studentService.getStudentByUserId(currentUser.getId());
            targetStudentId = studentDto.getId();
        }

        byte[] faceData;
        if (imageFile != null && !imageFile.isEmpty()) {
            faceData = imageFile.getBytes();
        } else if (body != null && body.containsKey("faceData")) {
            faceData = Base64.getDecoder().decode(body.get("faceData"));
        } else {
            // Default template marker if mock capture
            faceData = "FACE_TEMPLATE_VALIDATED".getBytes();
        }

        faceReferenceService.registerFaceReference(targetStudentId, faceData);
        return ResponseEntity.ok(ApiResponse.success("Face reference registered successfully", "OK"));
    }

    @GetMapping("/status/{studentId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFaceStatus(@PathVariable Long studentId) {
        boolean registered = faceReferenceService.hasFaceRegistered(studentId);
        return ResponseEntity.ok(ApiResponse.success(registered));
    }

    @GetMapping(value = "/{studentId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getFaceReference(@PathVariable Long studentId) {
        FaceReference reference = faceReferenceService.getFaceReference(studentId);
        return ResponseEntity.ok(reference.getReferenceData());
    }
}
