package org.delcom.app.views;

import org.delcom.app.entities.User;
import org.delcom.app.services.CashFlowService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeView {

    private final CashFlowService cashFlowService;

    public HomeView(CashFlowService cashFlowService) {
        this.cashFlowService = cashFlowService;
    }

    @GetMapping
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // CashFlow
        var cashFlows = cashFlowService.getAllCashFlows(authUser.getId(), "");
        model.addAttribute("cashFlows", cashFlows);

        int totalIn = cashFlows.stream().filter(c -> "CASH_IN".equals(c.getType()))
                .mapToInt(c -> c.getAmount()).sum();

        int totalOut = cashFlows.stream().filter(c -> "CASH_OUT".equals(c.getType()))
                .mapToInt(c -> c.getAmount()).sum();

        model.addAttribute("totalIn", totalIn);
        model.addAttribute("totalOut", totalOut);
        model.addAttribute("balance", totalIn - totalOut);

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }
}
