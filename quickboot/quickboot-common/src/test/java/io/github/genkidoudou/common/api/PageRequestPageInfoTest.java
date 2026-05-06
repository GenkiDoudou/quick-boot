package io.github.genkidoudou.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageRequestPageInfoTest {

    private static final ValidatorFactory VF = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VF.getValidator();

    @Test
    void pageRequest_defaultsAndOffset() {
        PageRequest<Void> p = new PageRequest<>();
        assertThat(p.getCurrent()).isEqualTo(1);
        assertThat(p.getSize()).isEqualTo(10);
        assertThat(p.getOffset()).isEqualTo(0);

        p.setCurrent(3);
        p.setSize(10);
        assertThat(p.getOffset()).isEqualTo(20);
    }

    @Test
    void pageRequest_currentZero_invalid() {
        Set<ConstraintViolation<PageRequest<Void>>> bad =
                VALIDATOR.validate(new PageRequest<>(0, 10, null));
        assertThat(bad).isNotEmpty();
    }

    @Test
    void pageRequest_sizeZero_invalid() {
        Set<ConstraintViolation<PageRequest<Void>>> bad =
                VALIDATOR.validate(new PageRequest<>(1, 0, null));
        assertThat(bad).isNotEmpty();
    }

    @Test
    void pageInfo_computePages_example() {
        assertThat(PageInfo.computePages(23, 10)).isEqualTo(3);
    }

    @Test
    void pageInfo_from_mybatisPlusPage() {
        Page<String> page = new Page<>(1, 10);
        page.setRecords(List.of("a", "b"));
        page.setTotal(23);

        PageInfo<String> info = PageInfo.from(page);
        assertThat(info.getCurrent()).isEqualTo(1);
        assertThat(info.getSize()).isEqualTo(10);
        assertThat(info.getRecords()).containsExactly("a", "b");
        assertThat(info.getTotal()).isEqualTo(23);
        assertThat(info.getPages()).isEqualTo(3);
    }

    @Test
    void pageInfo_from_null_throws() {
        assertThatThrownBy(() -> PageInfo.from(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
