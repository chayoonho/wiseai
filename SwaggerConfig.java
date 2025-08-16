import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "User API", description = "사용자 정보 관련 API")
public class UserController {

    @GetMapping("/api/users")
    @Operation(summary = "사용자 목록 조회", description = "시스템에 등록된 모든 사용자의 목록을 반환합니다.")
    public String getUsers() {
        return "사용자 목록";
    }
}