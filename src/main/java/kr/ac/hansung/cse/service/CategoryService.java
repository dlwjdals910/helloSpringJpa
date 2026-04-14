package kr.ac.hansung.cse.service;

import kr.ac.hansung.cse.model.Category;
import kr.ac.hansung.cse.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 카테고리 전체 목록 조회
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 카테고리 등록: 동일 이름 중복 시 예외 발생
    @Transactional
    public void createCategory(String name) {
        categoryRepository.findByName(name).ifPresent(c -> {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다: " + name);
        });
        categoryRepository.save(new Category(name));
    }

    // 카테고리 삭제: 연결된 상품이 있으면 예외 발생
    @Transactional
    public void deleteCategory(Long id) {
        long count = categoryRepository.countProductsByCategoryId(id);
        if (count > 0) {
            throw new IllegalStateException("해당 카테고리에 상품이 " + count + "개 연결되어 있어 삭제할 수 없습니다.");
        }
        categoryRepository.delete(id);
    }
}
