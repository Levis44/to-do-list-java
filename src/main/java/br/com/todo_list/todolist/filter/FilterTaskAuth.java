package br.com.todo_list.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.todo_list.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

 @Override
 protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

      var servletPath = request.getServletPath();

      if(!servletPath.startsWith("/tasks/")) {
        filterChain.doFilter(request, response);
        return;
      }

      var auth = request.getHeader("Authorization");
      var authEncoded = auth.substring("Basic".length()).trim();

      byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
      var authString = new String(authDecoded);

      String[] credencials = authString.split(":");

      String username = credencials[0];
      String password = credencials[1];

      var user = this.userRepository.findByUsername(username);

      if (user == null) {
        response.sendError(401);
        return;
      } 

      var correctPassword = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
      
      if (!correctPassword.verified) {
        response.sendError(401);
        return;
      }

      request.setAttribute("userId", user.getId());
      filterChain.doFilter(request, response);
    }
}
