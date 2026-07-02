package com.aiinterviewer.application.category;

import com.aiinterviewer.domain.category.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 카테고리 조회 유스케이스(읽기 전용). 세션 설정 화면이 선택지를 얻는 데 사용. */
@Service
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    public CategoryQueryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryView> findAll() {
        return categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(c -> new CategoryView(c.getSlug(), c.getName(), c.getPhase()))
                .toList();
    }
}
