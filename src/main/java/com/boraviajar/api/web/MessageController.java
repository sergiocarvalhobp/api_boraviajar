package com.boraviajar.api.web;

import com.boraviajar.api.entity.User;
import com.boraviajar.api.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/viagem/{viagemId}")
    public List<Map<String, Object>> list(@PathVariable long viagemId) {
        return messageService.listByViagem(viagemId, CurrentUser.optionalOrNull());
    }

    @PostMapping
    public Map<String, Object> send(@RequestBody SendMessageBody body) {
        User u = CurrentUser.require();
        return messageService.send(body.viagemId(), body.conteudo(), u);
    }

    public record SendMessageBody(long viagemId, String conteudo) {}
}
