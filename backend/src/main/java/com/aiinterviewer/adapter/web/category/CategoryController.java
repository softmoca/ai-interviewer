package com.aiinterviewer.adapter.web.category;

import com.aiinterviewer.application.category.CategoryQueryService;
import com.aiinterviewer.application.category.CategoryView;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 카테고리 조회 API(웹 어댑터). 인증 필요. 세션 설정 화면의 카테고리 선택지 제공.
 *
 * <ul>
 *   <li>GET /api/categories — 노출 순서대로 카테고리 목록</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryQueryService categoryQueryService;

    public CategoryController(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    @GetMapping
    public List<CategoryView> list() {
        return categoryQueryService.findAll();
    }
}
