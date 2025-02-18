package com.kalaiselvan.util;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    private SchedulerService schedulerService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        schedulerService = new SchedulerService();
        schedulerService.startScheduling();
        sce.getServletContext().setAttribute("schedulerService", schedulerService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (schedulerService != null) {
            schedulerService.shutdown();
        }
    }
}