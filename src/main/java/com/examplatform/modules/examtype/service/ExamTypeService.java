package com.examplatform.modules.examtype.service;

import com.examplatform.common.exception.DuplicateResourceException;
import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.common.exception.ValidationException;
import com.examplatform.modules.examtype.dto.request.ExamTypeRequest;
import com.examplatform.modules.examtype.dto.response.ExamTypeResponse;
import com.examplatform.modules.examtype.entity.ExamType;
import com.examplatform.modules.examtype.repository.ExamTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamTypeService {

    private final ExamTypeRepository examTypeRepository;

    public List<ExamTypeResponse> getAllExamTypes() {
        return examTypeRepository
                .findAllByIsActiveOrderByName(true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Super Admin panel: active + inactive সব category দেখানোর জন্য
    public List<ExamTypeResponse> getAllExamTypesAdmin() {
        return examTypeRepository
                .findAllByOrderByName()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExamTypeResponse getExamType(String id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ExamTypeResponse createExamType(ExamTypeRequest request) {
        if (examTypeRepository.existsByCode(
                request.getCode().toUpperCase())) {
            throw new DuplicateResourceException(
                "ExamType code already exists: " + request.getCode()
            );
        }

        ExamType examType = ExamType.builder()
                .name(request.getName())
                .nameBn(request.getNameBn())
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .conductingBody(request.getConductingBody())
                .build();

        return toResponse(examTypeRepository.save(examType));
    }

    @Transactional
    public ExamTypeResponse updateExamType(String id,
                                            ExamTypeRequest request) {
        ExamType examType = findById(id);

        // Code বদলানো নিষিদ্ধ: exam (education_level / target_levels) আর user (target_exam)-এ
        // এই code String হিসেবে জমা থাকে, বদলালে ওই সব ডাটা অনাথ হয়ে যায়।
        // Frontend এ আগে থেকেই lock করা আছে, এখানে API লেভেলেও আটকানো হলো।
        if (!examType.getCode().equalsIgnoreCase(request.getCode())) {
            throw new ValidationException(
                "Category এর code বদলানো যাবে না (বর্তমান code: " + examType.getCode() + "). "
                + "ভুল হলে নতুন category বানিয়ে পুরনোটা নিষ্ক্রিয় করুন।"
            );
        }

        examType.setName(request.getName());
        examType.setNameBn(request.getNameBn());
        examType.setDescription(request.getDescription());
        examType.setConductingBody(request.getConductingBody());
        if (request.getIsActive() != null) {
            examType.setActive(request.getIsActive());
        }

        return toResponse(examTypeRepository.save(examType));
    }

    @Transactional
    public void deleteExamType(String id) {
        ExamType examType = findById(id);
        examType.setActive(false);
        examTypeRepository.save(examType);
    }

    private ExamType findById(String id) {
        return examTypeRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("ExamType", id));
    }

    private ExamTypeResponse toResponse(ExamType e) {
        return ExamTypeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .nameBn(e.getNameBn())
                .code(e.getCode())
                .description(e.getDescription())
                .conductingBody(e.getConductingBody())
                .isActive(e.isActive())
                .build();
    }
}