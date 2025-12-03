package org.delcom.app.views;

import java.util.List;
import java.util.UUID;

import org.delcom.app.dto.CashFlowForm; 
import org.delcom.app.entities.CashFlow;
import org.delcom.app.entities.User;
import org.delcom.app.services.CashFlowService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cash-flows")
public class CashFlowView {

    private final CashFlowService cashFlowService;

    public CashFlowView(CashFlowService cashFlowService) {
        this.cashFlowService = cashFlowService;
    }

    private User getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken)
            return null;
        Object principal = authentication.getPrincipal();
        return (principal instanceof User) ? (User) principal : null;
    }

    @GetMapping
    public String listCashFlows(@RequestParam(required = false) String search, Model model) {
        User user = getAuthUser();
        if (user == null)
            return "redirect:/auth/logout";

        List<CashFlow> cashFlows = cashFlowService.getAllCashFlows(user.getId(), search);

        int totalIn = cashFlows.stream().filter(c -> "CASH_IN".equals(c.getType())).mapToInt(CashFlow::getAmount).sum();
        int totalOut = cashFlows.stream().filter(c -> "CASH_OUT".equals(c.getType())).mapToInt(CashFlow::getAmount)
                .sum();

        model.addAttribute("cashFlows", cashFlows);
        model.addAttribute("search", search);
        model.addAttribute("totalIn", totalIn);
        model.addAttribute("totalOut", totalOut);
        model.addAttribute("balance", totalIn - totalOut);
        model.addAttribute("user", user);

        return "pages/cash-flows/home";
    }

    // Pakai CashFlowForm ===
    @GetMapping("/add")
    public String addCashFlowPage(Model model) {
        User user = getAuthUser();
        if (user == null)
            return "redirect:/auth/logout";

        model.addAttribute("cashFlowForm", new CashFlowForm()); // Kirim Form Kosong
        return "pages/cash-flows/form";
    }

    // Terima CashFlowForm ===
    @PostMapping("/add")
    public String postAddCashFlow(@ModelAttribute("cashFlowForm") CashFlowForm form,
            RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null)
            return "redirect:/auth/logout";

        // Validasi Manual (Bisa juga pakai @Valid)
        if (form.getAmount() == null || form.getAmount() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Nominal harus lebih dari 0");
            return "redirect:/cash-flows/add";
        }

        // Panggil Service pakai data dari Form
        cashFlowService.createCashFlow(
                user.getId(),
                form.getType(),
                form.getSource(),
                form.getLabel(),
                form.getAmount(),
                form.getDescription());

        redirectAttributes.addFlashAttribute("success", "Transaksi berhasil ditambahkan.");
        return "redirect:/cash-flows";
    }

    // Mapping Entity ke Form 
    @GetMapping("/edit/{id}")
    public String editCashFlowPage(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null)
            return "redirect:/auth/logout";

        CashFlow cashFlow = cashFlowService.getCashFlowById(user.getId(), id);
        if (cashFlow == null) {
            redirectAttributes.addFlashAttribute("error", "Data tidak ditemukan.");
            return "redirect:/cash-flows";
        }

        // Mapping Data Entity -> Form
        CashFlowForm form = new CashFlowForm();
        form.setId(cashFlow.getId());
        form.setType(cashFlow.getType());
        form.setSource(cashFlow.getSource());
        form.setLabel(cashFlow.getLabel());
        form.setAmount(cashFlow.getAmount());
        form.setDescription(cashFlow.getDescription());

        model.addAttribute("cashFlowForm", form); // Kirim Form yang sudah terisi
        return "pages/cash-flows/form";
    }

    // Terima CashFlowForm
    @PostMapping("/edit/{id}")
    public String postEditCashFlow(@PathVariable UUID id, @ModelAttribute("cashFlowForm") CashFlowForm form,
            RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null)
            return "redirect:/auth/logout";

        cashFlowService.updateCashFlow(
                user.getId(), id,
                form.getType(),
                form.getSource(),
                form.getLabel(),
                form.getAmount(),
                form.getDescription());

        redirectAttributes.addFlashAttribute("success", "Transaksi berhasil diperbarui.");
        return "redirect:/cash-flows";
    }

    // Delete tetap sama karena hanya butuh ID
    @PostMapping("/delete/{id}")
    public String postDeleteCashFlow(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null)
            return "redirect:/auth/logout";
        cashFlowService.deleteCashFlow(user.getId(), id);
        redirectAttributes.addFlashAttribute("success", "Transaksi berhasil dihapus.");
        return "redirect:/cash-flows";
    }
}