package cz.cvut.fit.valespe.migration.command;

import cz.cvut.fit.valespe.migration.MigrationEntity;
import cz.cvut.fit.valespe.migration.operation.MigrationSetupOperations;
import cz.cvut.fit.valespe.migration.operation.NewPropertyOperations;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

@Component
@Service
public class NewPropertyCommands implements CommandMarker {

    private static final JavaType MIGRATION_ENTITY_ANNOTATION = new JavaType(MigrationEntity.class.getName());
    
    @Reference private NewPropertyOperations newPropertyOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private ProjectOperations projectOperations;
    @Reference private MigrationSetupOperations migrationSetupOperations;

    public NewPropertyCommands() {}

    public NewPropertyCommands(NewPropertyOperations newPropertyOperations, ProjectOperations projectOperations, MigrationSetupOperations migrationSetupOperations, TypeLocationService typeLocationService) {
        this.newPropertyOperations = newPropertyOperations;
        this.projectOperations = projectOperations;
        this.migrationSetupOperations = migrationSetupOperations;
        this.typeLocationService = typeLocationService;
    }

    @CliAvailabilityIndicator({ "migrate new property" })
    public boolean isCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable() && migrationSetupOperations.doesMigrationFileExist();
    }
    
    @CliCommand(value = "migrate new property", help = "Some helpful description")
    public void newProperty(
            @CliOption(key = "class", mandatory = true, unspecifiedDefaultValue = "*", optionContext = UPDATE_PROJECT, help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "property", mandatory = true, help = "The name of the field to newProperty") final JavaSymbolName propertyName,
            @CliOption(key = "propertyType", mandatory = true, help = "Type of new property") final JavaType propertyType,
            @CliOption(key = "column", mandatory = true, help = "The JPA @Column name") final String columnName,
            @CliOption(key = "columnType", mandatory = true, help = "The JPA @Column name") final String columnType) {
        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService.getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '%s', doesn't exist", typeName);

        newPropertyOperations.addFieldToClass(propertyName, propertyType, columnName, columnType, javaTypeDetails);
        addColumn(columnName, columnType, javaTypeDetails);
    }

    private void addColumn(String columnName, String columnType, ClassOrInterfaceTypeDetails javaTypeDetails) {
        AnnotationMetadata migrationEntity = javaTypeDetails.getAnnotation(MIGRATION_ENTITY_ANNOTATION);
        AnnotationAttributeValue<String> table = migrationEntity.getAttribute("table");
        AnnotationAttributeValue<String> schema = migrationEntity.getAttribute("schema");
        AnnotationAttributeValue<String> catalog = migrationEntity.getAttribute("catalog");
        newPropertyOperations.createColumn(
                table == null ? "" : table.getValue(),
                schema == null ? "" : schema.getValue(),
                catalog == null ? "" : catalog.getValue(),
                columnName, columnType
        );
    }

}