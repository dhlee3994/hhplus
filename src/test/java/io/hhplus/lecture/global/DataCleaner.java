package io.hhplus.lecture.global;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Type;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataCleaner {

	private final EntityManager entityManager;
	private final List<String> tableNames;

	public DataCleaner(final EntityManager entityManager) {
		this.entityManager = entityManager;
		this.tableNames = entityManager.getMetamodel()
			.getEntities()
			.stream()
			.map(Type::getJavaType)
			.filter(javaType -> javaType.getAnnotation(Table.class) != null)
			.map(javaType -> javaType.getAnnotation(Table.class))
			.map(Table::name)
			.collect(Collectors.toList());
	}

	@Transactional
	public void cleaning() {
		entityManager.flush();
		for (String tableName : tableNames) {
			entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
		}
	}
}
