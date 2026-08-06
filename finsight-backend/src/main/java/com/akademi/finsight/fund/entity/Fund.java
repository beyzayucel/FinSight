package com.akademi.finsight.fund.entity;

import com.akademi.finsight.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "funds", uniqueConstraints = {
		@UniqueConstraint(name = "uk_funds_code", columnNames = "code")
})
@SQLDelete(sql = "UPDATE funds SET deleted = 1, deleted_at = SYSDATETIMEOFFSET() WHERE id = ?")
public class Fund extends SoftDeletableEntity {

	@Column(nullable = false, length = 3)
	private String code;

	@Column
	private String name;
}
