package kr.ac.hansung.cse.controller;

import jakarta.validation.Valid;
import kr.ac.hansung.cse.exception.ProductNotFoundException;
import kr.ac.hansung.cse.model.Product;
import kr.ac.hansung.cse.model.ProductForm;
import kr.ac.hansung.cse.service.CategoryService;
import kr.ac.hansung.cse.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // GET /products - 상품 목록 + 검색/필터
    @GetMapping
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "categoryId", required = false) Long categoryId,
                               Model model) {
        List<Product> products = productService.searchProducts(keyword, categoryId);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("selectedCategoryId", categoryId);
        return "productList";
    }

    // GET /products/{id} - 상품 상세 조회
    @GetMapping("/{id}")
    public String showProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        model.addAttribute("product", product);
        return "productView";
    }

    // GET /products/create - 상품 등록 폼
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "productForm";
    }

    // POST /products/create - 상품 등록 처리
    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "productForm";
        }
        Product product = productForm.toEntity();
        product.setCategory(productService.resolveCategory(productForm.getCategory()));
        Product savedProduct = productService.createProduct(product);

        redirectAttributes.addFlashAttribute("successMessage",
                "'" + savedProduct.getName() + "' 상품이 성공적으로 등록되었습니다.");
        return "redirect:/products";
    }

    // GET /products/{id}/edit - 상품 수정 폼
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        model.addAttribute("productForm", ProductForm.from(product));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "productEditForm";
    }

    // POST /products/{id}/edit - 상품 수정 처리
    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productForm") ProductForm productForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "productEditForm";
        }
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(productForm.getName());
        product.setCategory(productService.resolveCategory(productForm.getCategory()));
        product.setPrice(productForm.getPrice());
        product.setDescription(productForm.getDescription());
        productService.updateProduct(product);

        redirectAttributes.addFlashAttribute("successMessage",
                "'" + product.getName() + "' 상품 정보가 수정되었습니다.");
        return "redirect:/products/" + id;
    }

    // POST /products/{id}/delete - 상품 삭제 처리
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        String productName = product.getName();
        productService.deleteProduct(id);

        redirectAttributes.addFlashAttribute("successMessage",
                "'" + productName + "' 상품이 삭제되었습니다.");
        return "redirect:/products";
    }
}
