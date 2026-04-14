package kr.ac.hansung.cse.controller;

import kr.ac.hansung.cse.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // GET /categories - 카테고리 목록 조회
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categoryList";
    }

    // GET /categories/create - 카테고리 등록 폼 표시
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // 오류 메시지가 없을 때 null 방지용 빈 문자열 세팅
        if (!model.containsAttribute("categoryName")) {
            model.addAttribute("categoryName", "");
        }
        return "categoryForm";
    }

    // POST /categories/create - 카테고리 등록 처리
    @PostMapping("/create")
    public String createCategory(@RequestParam("name") String name,
                                 RedirectAttributes redirectAttributes) {
        // 빈 이름 검증
        if (name == null || name.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "카테고리 이름을 입력해 주세요.");
            redirectAttributes.addFlashAttribute("categoryName", name);
            return "redirect:/categories/create";
        }

        try {
            categoryService.createCategory(name.trim());
            redirectAttributes.addFlashAttribute("successMessage",
                    "'" + name.trim() + "' 카테고리가 등록되었습니다.");
            return "redirect:/categories";
        } catch (IllegalArgumentException e) {
            // 중복 이름 예외 처리: 폼으로 돌아가서 오류 메시지 표시
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("categoryName", name);
            return "redirect:/categories/create";
        }
    }

    // POST /categories/{id}/delete - 카테고리 삭제 처리
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "카테고리가 삭제되었습니다.");
        } catch (IllegalStateException e) {
            // 연결된 상품이 있을 경우 오류 메시지 표시
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }
}
