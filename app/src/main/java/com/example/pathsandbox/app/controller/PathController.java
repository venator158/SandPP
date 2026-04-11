package com.example.pathsandbox.app.controller;

import com.example.pathsandbox.app.model.PathExecutionLog;
import com.example.pathsandbox.app.repository.PathExecutionLogRepository;
import com.example.pathsandbox.app.service.PathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PathController {

    @Autowired
    private PathService pathService;

    @Autowired
    private PathExecutionLogRepository logRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/execute")
    public String executePathfinding(
            @RequestParam("inputMap") String inputMap,
            @RequestParam("startCoord") String startCoord,
            @RequestParam("goalCoord") String goalCoord,
            @RequestParam("plannerName") String plannerName,
            @RequestParam(value = "diagonals", defaultValue = "false") boolean diagonals,
            Model model) {
        
        try {
            PathExecutionLog log = pathService.solvePath(inputMap, startCoord, goalCoord, plannerName, diagonals);
            model.addAttribute("log", log);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "result";
    }

    @GetMapping("/history")
    public String viewHistory(Model model) {
        List<PathExecutionLog> logs = logRepository.findAll();
        model.addAttribute("logs", logs);
        return "history";
    }
}
