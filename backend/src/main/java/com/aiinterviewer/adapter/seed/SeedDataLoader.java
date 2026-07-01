package com.aiinterviewer.adapter.seed;

import com.aiinterviewer.domain.category.Category;
import com.aiinterviewer.domain.category.CategoryPhase;
import com.aiinterviewer.domain.category.CategoryRepository;
import com.aiinterviewer.domain.question.Question;
import com.aiinterviewer.domain.question.QuestionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개발용 seed 적재기. 클래스패스의 {@code seed/*.json}을 읽어 카테고리/질문을 DB에 적재한다
 * (결정사항 D13/D24). **카테고리 하드코딩이 없다** — 카테고리는 파일에서 유도되므로, 새
 * 카테고리 파일(예: network.json)만 추가하면 코드 변경 없이 적재된다.
 *
 * <p>유도 규칙: 파일명(확장자 제외)=슬러그, JSON의 {@code category}=카테고리명, phase는 현재
 * 모두 A안이라 {@link CategoryPhase#MVP} 기본값, 노출 순서는 파일명 정렬 순.
 * 이미 질문이 있으면(=적재됨) 건너뛴다(멱등).
 */
@Component
public class SeedDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);
    private static final String SEED_LOCATION = "classpath*:seed/*.json";

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourceResolver;

    public SeedDataLoader(CategoryRepository categoryRepository, QuestionRepository questionRepository,
                          ObjectMapper objectMapper, ResourcePatternResolver resourceResolver) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
        this.resourceResolver = resourceResolver;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        if (questionRepository.count() > 0) {
            log.info("seed 적재 건너뜀 — 이미 질문 {}건 존재", questionRepository.count());
            return;
        }

        Resource[] resources = resourceResolver.getResources(SEED_LOCATION);
        Arrays.sort(resources, Comparator.comparing(r -> safeName(r)));

        int sortOrder = 0;
        int loadedQuestions = 0;
        for (Resource resource : resources) {
            String slug = slugOf(resource);
            List<SeedQuestion> items = read(resource);
            if (items.isEmpty()) {
                continue;
            }
            Category category = resolveCategory(slug, items.get(0).category(), sortOrder++);
            for (SeedQuestion item : items) {
                questionRepository.save(Question.of(category, item.topic(), item.content(),
                        item.difficulty(), item.keywords(), item.modelAnswer(), item.sourceUrl(),
                        item.opening()));
                loadedQuestions++;
            }
        }
        log.info("seed 적재 완료 — 카테고리 {}개, 질문 {}건", sortOrder, loadedQuestions);
    }

    private Category resolveCategory(String slug, String name, int sortOrder) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(
                        Category.of(name, slug, CategoryPhase.MVP, sortOrder)));
    }

    private List<SeedQuestion> read(Resource resource) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readValue(in, new TypeReference<List<SeedQuestion>>() {
            });
        }
    }

    private String slugOf(Resource resource) {
        String name = safeName(resource);
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private String safeName(Resource resource) {
        String name = resource.getFilename();
        return name == null ? "" : name;
    }
}
