package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.*;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;
import static java.util.stream.Collectors.toSet;

public class MainXml {

    private static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getValue).thenComparing(User::getEmail);

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalAccessException("Expected ProjectName");
        }
        String projectName = args[0];
        URL payloadURL = Resources.getResource("payload.xml");
        //Set<User> users = parseByJaxb(projectName, payloadURL);
        Set<User> users = parseByStax(projectName, payloadURL);
        String output = outHtml(users, projectName, Paths.get("out/usersJaxb.html"));
        System.out.println(output);

        String html = processByXslt(projectName, payloadURL);
    }

    private static String processByXslt(String projectName, URL payloadURL) throws IOException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        URL xsltURL = Resources.getResource("users.xslt");
        Source xsltSource = new StreamSource(new FileReader(xsltURL.getFile()));
        Transformer xsltTransformer = transformerFactory.newTransformer(xsltSource);
        xsltTransformer.setParameter("projectName", projectName);
        xsltTransformer.transform(new StreamSource(new FileReader(payloadURL.getFile())),
                new StreamResult(new FileWriter("out/usersXslt.html")));
        return "";
    }

    private static Set<User> parseByJaxb(String projectName, URL payloadURL) throws IOException, JAXBException {
        JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));

        try (InputStream is = payloadURL.openStream()) {
            Payload payload = JAXB_PARSER.unmarshal(is);
            Project project = payload.getProjects().getProject().stream()
                    .filter(p -> p.getName().equals(projectName))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project name '" + projectName + "'"));

            Set<Group> groups = project
                    .getGroups()
                    .getGroup()
                    .stream()
                    .collect(toSet());

            return payload.getUsers().getUser()
                    .stream()
                    .filter((u) -> u.getGroupRefs().stream()
                            .filter((g) -> groups.contains((Group) g))
                            .findAny()
                            .isPresent())
                    .collect(Collectors.toCollection(() -> new TreeSet<>(USER_COMPARATOR)));
        }
    }

    private static String outHtml(Set<User> users, String projectName, Path path) throws IOException {
        String result = body().with(
                h1(projectName),
                table().
                        with(

                                users.stream().map(
                                        u -> tr().with(
                                                td(u.getValue()),
                                                td(u.getEmail())

                                        )
                                ).collect(Collectors.toList())

                        )
        ).render();
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            fileWriter.write(result);
        }

        return result;
    }

    private static Set<User> parseByStax(String projectName, URL payloadURL) throws IOException, XMLStreamException {
        Set<User> result = new TreeSet<>(USER_COMPARATOR);

        try (StaxStreamProcessor processor = new StaxStreamProcessor(payloadURL.openStream())) {
            XMLStreamReader reader = processor.getReader();
            String currentProject = "";
            List<String> groups = new ArrayList<>();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if ("Project".equals(reader.getLocalName())) {
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            if ("name".equals(reader.getAttributeLocalName(i))) {
                                currentProject = reader.getAttributeValue(i);
                            }
                        }
                    }

                    if ("Group".equals(reader.getLocalName())) {
                        if (currentProject.equals(projectName)) {
                            for (int i = 0; i < reader.getAttributeCount(); i++) {
                                if ("name".equals(reader.getAttributeLocalName(i))) {
                                    groups.add(reader.getAttributeValue(i));
                                }
                            }
                        }
                    }

                    if ("User".equals(reader.getLocalName())) {
                        String email = "";
                        String groupRefs = "";
                        String userName = "";
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            if ("groupRefs".equals(reader.getAttributeLocalName(i))) {
                                groupRefs = reader.getAttributeValue(i);
                            }

                            if ("email".equals(reader.getAttributeLocalName(i))) {
                                email = reader.getAttributeValue(i);
                            }
                        }
                        userName = reader.getElementText();

                        if (!groupRefs.isEmpty()) {
                            String[] groupNames = groupRefs.split(" ");
                            for (String groupName : groupNames) {
                                if (groups.contains(groupName)) {
                                    User user = new User();
                                    user.setEmail(email);
                                    user.setValue(userName);
                                    result.add(user);
                                }
                            }
                        }
                    }

                }
            }
        }
        return result;

    }

}
