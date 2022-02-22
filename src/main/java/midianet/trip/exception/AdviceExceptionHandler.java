package midianet.trip.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.AllArgsConstructor;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static midianet.trip.util.MessageUtil.getMessage;

@AllArgsConstructor
@RestControllerAdvice
public class AdviceExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String DEFAULT_VALIDATION_MESSAGE = "cabala.message.default.validation";

//400
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final var errors = new ArrayList<String>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + checkMessage(error.getDefaultMessage()));
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + checkMessage(error.getDefaultMessage()));
        }
        final var error = ErrorResponse.builder().message(getMessage(DEFAULT_VALIDATION_MESSAGE)).details(errors).build();
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    private String checkMessage(@Nullable final String message){
        if(Objects.nonNull(message) && message.contains("Failed to convert property value")) return "Valor inválido";
        return message;
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        final var errors = new ArrayList<String>();
        if(ex instanceof MethodArgumentConversionNotSupportedException ) {
            errors.add(((MethodArgumentConversionNotSupportedException)ex).getName() + ": " + ex.getValue());
        }
        final var error = ErrorResponse.builder().message("Ocorreu um erro de conversão").details(errors).build();
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(final BindException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final var errors = new ArrayList<String>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + checkMessage(error.getDefaultMessage()));
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + checkMessage(error.getDefaultMessage()));
        }
        final var error = ErrorResponse.builder().message(getMessage(DEFAULT_VALIDATION_MESSAGE)).details(errors).build();
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        var errors = new ArrayList<String>();
        if (ex.getCause() instanceof InvalidFormatException)  errors.addAll(handleInvalidFormatException((InvalidFormatException) ex.getCause()));
        final var error = ErrorResponse.builder().message(getMessage(DEFAULT_VALIDATION_MESSAGE)).details(errors).build();
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object>  handleTypeMismatch(final TypeMismatchException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final String errors = ex.getPropertyName()  + ": Valor " +  ex.getValue() + " inválido, deve ser do  tipo "  + ex.getRequiredType();
        final var error = ErrorResponse.builder().message(getMessage(DEFAULT_VALIDATION_MESSAGE)).details(List.of(errors)).build();
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object>  handleMissingServletRequestPart(final MissingServletRequestPartException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final String errors = ex.getRequestPartName() + ": Não foi informado";
        final var error = ErrorResponse.builder().message(getMessage(DEFAULT_VALIDATION_MESSAGE)).details(List.of(errors)).build();
        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object>  handleMissingServletRequestParameter(final MissingServletRequestParameterException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final String errors = ex.getParameterName() + ": Não foi informado";
        final var error = ErrorResponse.builder().message(getMessage(DEFAULT_VALIDATION_MESSAGE)).details(List.of(errors)).build();
        return handleExceptionInternal(ex, error, headers, status, request);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
    public ErrorResponse handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex, final WebRequest request) {
        var typeName = "";
        if(Objects.nonNull(ex) && Objects.nonNull(ex.getRequiredType())){
            var type = ex.getRequiredType();
            if(Objects.nonNull(type)) typeName = type.getName();
        }
        final var errors = Objects.nonNull(ex)? ex.getName() : ""  + ": Deverá ser do tipo " + typeName;
        return ErrorResponse.builder().message(getMessage(DEFAULT_VALIDATION_MESSAGE)).details(List.of(errors)).build();
    }

    private List<String> handleInvalidFormatException(final InvalidFormatException ex){
        final var errors = new ArrayList<String>();
        for (final JsonMappingException.Reference violation : ex.getPath()) {
            if(Objects.nonNull(violation.getFieldName())) errors.add(violation.getFieldName() + ": " + getMessage("cabala.message.default.invalid"));
        }
        return errors;
    }

    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ExceptionHandler({ ConstraintViolationException.class })
    public ErrorResponse handleConstraintViolation(final ConstraintViolationException ex, final WebRequest request) {
        final var errors = new ArrayList<String>();
        for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        return ErrorResponse.builder().message("Ocorreu um erro de validação").details(errors).build();
    }

    // 404
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final var errors = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        final var error = ErrorResponse.builder().message(getMessage(DEFAULT_VALIDATION_MESSAGE)).details(List.of(errors)).build();
        return new ResponseEntity<>(error, new HttpHeaders(), status);
    }

    // 405
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(final HttpRequestMethodNotSupportedException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(" Método não permitido para essa requisição. Métodos suportados são ");
        Optional.ofNullable(ex.getSupportedHttpMethods())
            .ifPresent(httpMethods -> httpMethods.forEach(t -> builder.append(t + " ")));
        final var error = ErrorResponse.builder().message(getMessage(DEFAULT_VALIDATION_MESSAGE)).details(List.of(builder.toString())).build();
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 415
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" Tipo não suportado. Tipos Suportados são ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t + " "));
        final var error = ErrorResponse.builder().message(getMessage(DEFAULT_VALIDATION_MESSAGE)).details(List.of(builder.substring(0, builder.length() - 2))).build();
        return new ResponseEntity<>(error, new HttpHeaders(),HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // 412
    @ExceptionHandler({ BusinessException.class ,IllegalArgumentException.class })
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public ErrorResponse handlePrecondition(final Exception ex, final WebRequest request) {
        return ErrorResponse.builder().message(ex.getLocalizedMessage()).build();
    }

//    @ExceptionHandler({UnauthorizedException.class})
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    public ErrorResponse handleUnauthorizedException(final Exception ex, final WebRequest request) {
//        return ErrorResponse.builder().message(ex.getLocalizedMessage()).build();
//    }

    // 404
    @ExceptionHandler({EntityNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final Exception e, final WebRequest request) {
        return ErrorResponse.builder().message(e.getLocalizedMessage()).build();
    }

    // 400
    @ExceptionHandler({RestClientException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRestClientException(final RestClientException e, final WebRequest request) {
        return ErrorResponse.builder().message(e.getLocalizedMessage()).build();
    }
    // 500
    @ExceptionHandler({ Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll(final Exception e, final WebRequest request) {
        return ErrorResponse.builder().message(e.getLocalizedMessage()).build();
    }

}
