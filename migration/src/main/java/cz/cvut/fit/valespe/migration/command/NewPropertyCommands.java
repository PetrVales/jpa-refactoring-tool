package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.operation.LiquibaseOperations;
import cz.cvut.fit.valespe.migration.operation.PropertyOperations;
import cz.cvut.fit.valespe.migration.util.ClassCommons;
import cz.cvut.fit.valespe.migration.util.impl.ClassCommonsImpl;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

@Component
@Service
public class NewPropertyCommands implements CommandMarker {
    @Reference private PropertyOperations propertyOperations;
    @Reference private ProjectOperations projectOperations;
    @Reference private LiquibaseOperations liquibaseOperations;
    @Reference private ClassCommons classCommons;


    public NewPropertyCommands() {}

    public NewPropertyCommands(
            PropertyOperations propertyOperations,
            ProjectOperations projectOperations,
            LiquibaseOperations liquibaseOperations,
            ClassCommons classCommons
    ) {
        this.propertyOperations = propertyOperations;
        this.projectOperations = projectOperations;
        this.liquibaseOperations = liquibaseOperations;
        this.classCommons = classCommons;
    }

    @CliAvailabilityIndicator({ "migrate new property" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && liquibaseOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migrate new property", help = "Some helpful description")
    public void newProperty(
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = {"", "property"}, mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "propertyType", mandatory = true, help = "Type of new property") final JavaType propertyType,
            @CliOption(key = "column", mandatory = true, help = "The JPA @Column name") final String columnName,
            @CliOption(key = "columnType", mandatory = true, help = "The JPA @Column name") final String columnType,
            @CliOption(key = "pk", mandatory = false, help = "@Id", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final Boolean pk,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        Validate.isTrue(classCommons.exist(typeName), "Specified class, '%s', doesn't exist", typeName);

        propertyOperations.addField(propertyName, propertyType, columnName, columnType, typeName, pk);
        addColumn(typeName, columnName, columnType, pk, author, id);
    }

    @CliCommand(value = "migrate add id", help = "Some helpful description")
    public void addId(
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        Validate.isTrue(classCommons.exist(typeName), "Specified class, '%s', doesn't exist", typeName);

        propertyOperations.addField(new JavaSymbolName("id"), new JavaType("java.lang.Long"), "id", "bigint", typeName, true);
        addColumn(typeName, "id", "bigint", true, author, id);
    }

    @CliCommand(value = "migrate add string", help = "Some helpful description")
    public void addString(
            @CliOption(key = {"field", ""}, mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName field,
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "column", mandatory = true, help = "The JPA @Column name") final String columnName,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        Validate.isTrue(classCommons.exist(typeName), "Specified class, '%s', doesn't exist", typeName);

        propertyOperations.addField(field, new JavaType("java.lang.String"), columnName, "varchar2(255)", typeName);
        addColumn(typeName, columnName, "varchar2(255)", false, author, id);
    }

    @CliCommand(value = "migrate add integer", help = "Some helpful description")
    public void addInteger(
            @CliOption(key = {"field", ""}, mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName field,
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "column", mandatory = true, help = "The JPA @Column name") final String columnName,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        Validate.isTrue(classCommons.exist(typeName), "Specified class, '%s', doesn't exist", typeName);

        propertyOperations.addField(field, new JavaType("java.lang.Integer"), columnName, "integer", typeName);
        addColumn(typeName, columnName, "integer", false, author, id);
    }

    @CliCommand(value = "migrate add boolean", help = "Some helpful description")
    public void addBoolean(
            @CliOption(key = {"field", ""}, mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName field,
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "column", mandatory = true, help = "The JPA @Column name") final String columnName,
            @CliOption(key = "author", mandatory = false, help = "author") final String author,
            @CliOption(key = "id", mandatory = false, help = "id") final String id
    ) {
        Validate.isTrue(classCommons.exist(typeName), "Specified class, '%s', doesn't exist", typeName);

        propertyOperations.addField(field, new JavaType("java.lang.Boolean"), columnName, "boolean", typeName);
        addColumn(typeName, columnName, "boolean", false, author, id);
    }

    private void addColumn(JavaType typeName, String columnName, String columnType, Boolean pk, String author, String id) {
        final String tableName = classCommons.tableName(typeName);
        final List<Element> elements = new LinkedList<Element>();
        elements.add(liquibaseOperations.addColumn(tableName, columnName, columnType));
        if (pk != null && pk)
            elements.add(liquibaseOperations.addPrimaryKey(Arrays.asList(columnName), tableName, columnName + "_pk"));
        liquibaseOperations.createChangeSet(elements, author, id);
    }

}
