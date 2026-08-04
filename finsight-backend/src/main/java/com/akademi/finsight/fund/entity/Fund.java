package com.akademi.finsight.fund.entity;

import com.akademi.finsight.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "funds", uniqueConstraints = {
		@UniqueConstraint(name = "uk_funds_code", columnNames = "code")
})
public class Fund extends BaseEntity {

	@Column(nullable = false, length = 3)
	private String code;

	@Column(nullable = false)
	private LocalDate date;
}
