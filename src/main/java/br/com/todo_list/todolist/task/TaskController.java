package br.com.todo_list.todolist.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.todo_list.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest httpServletRequest){
    var userId = httpServletRequest.getAttribute("userId");
    taskModel.setUserId((UUID)userId);

    var currentDate = LocalDateTime.now();

    Boolean isStartAtAfterToday = currentDate.isAfter(taskModel.getStartAt());
    Boolean isEndAtAfterToday = currentDate.isAfter(taskModel.getEndAt());
    Boolean isStartAtAfterEndAt = taskModel.getStartAt().isAfter(taskModel.getEndAt());
    
    if ( isStartAtAfterToday || isEndAtAfterToday){
      return ResponseEntity
                          .status(HttpStatus.BAD_REQUEST)
                          .body("A data de início / data de término deve ser maior do que a data atual");
    }

    if (isStartAtAfterEndAt){
      return ResponseEntity
                          .status(HttpStatus.BAD_REQUEST)
                          .body("A data início deve ser antes da data de término");
    }

    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.CREATED).body(task);
  }

  @GetMapping("/")
  public ResponseEntity get(HttpServletRequest httpServletRequest) {
    var userId = httpServletRequest.getAttribute("userId");

    var tasks = this.taskRepository.findByUserId((UUID) userId);

    return ResponseEntity.status(HttpStatus.OK).body(tasks);
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest httpServletRequest) {
    
    var task = this.taskRepository.findById(id).orElse(null);
    
    if(task == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarefa não encontrada");
    }
    
    var userId = httpServletRequest.getAttribute("userId");
    
    if(!task.getUserId().equals(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário sem permissão para alterar a tarefa");

    }

    Utils.copyNonNullProperties(taskModel, task);

    var taskUpdated = this.taskRepository.save(task);
    return ResponseEntity.status(HttpStatus.OK).body(taskUpdated);
  }
}
