package io.mikael.nioth2015;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class Application {

    @Autowired
    private EventProcessor eventProcessor;

    @Autowired
    private EventStreamConnection connection;

    @RequestMapping(value="/events/", method=RequestMethod.POST)
    public ResponseEntity<TemperatureEvent> createEvent(final @RequestBody TemperatureEvent event) {
        eventProcessor.submitEvent(event);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @RequestMapping(value="/analysis/", method=RequestMethod.GET)
    public ResponseEntity<Map> showAnalysis() {
        return new ResponseEntity<>(ImmutableMap.of(
                        "min", eventProcessor.min,
                        "max", eventProcessor.max,
                        "avg", eventProcessor.avg,
                        "latest", eventProcessor.latest),
                HttpStatus.OK);
    }

    @RequestMapping(value="/lists/", method=RequestMethod.GET)
    public ResponseEntity<List<SensorAnalysis>> showLists() {
        return new ResponseEntity<>(eventProcessor.readLists(), HttpStatus.OK);
    }

    @RequestMapping(value="/alerts/{id}", method=RequestMethod.POST)
    public ResponseEntity<String> setStatus(final @PathVariable("id") String id, final @RequestBody String alert) {
        eventProcessor.alerts.put(id, alert);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @RequestMapping(value="/emoticons/{id}", method=RequestMethod.POST)
    public ResponseEntity<String> setEmoticon(final @PathVariable("id") String id, final @RequestBody String emoticon) {
        eventProcessor.emoticons.put(id, Integer.parseInt(emoticon));
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @Bean
    public Filter corsFilter() {
        return new Filter() {
            @Override
            public void init(final FilterConfig filterConfig) throws ServletException {
            }

            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
                final HttpServletResponse response = (HttpServletResponse) res;
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
                chain.doFilter(req, res);
            }

            @Override
            public void destroy() {
            }
        };
    }

    public static void main(final String ... args) {
        SpringApplication.run(Application.class, args);
    }

}
