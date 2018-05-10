package louie.share.druid.web;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author louie
 * @date created in 2018-5-10 14:38
 */
@RestController
public class DruidController {
    @Autowired
    IdentityService identityService;

    @RequestMapping(value = "/users/{userId}")
    public User findById(@PathVariable String userId){
        return identityService.createUserQuery().userId(userId).singleResult();
    }
}
