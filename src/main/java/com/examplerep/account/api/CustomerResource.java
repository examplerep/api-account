package com.examplerep.account.api;

import com.examplerep.account.CustomerType;
import com.examplerep.account.Roles;
import com.examplerep.account.service.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Customer")
public class CustomerResource {

    @Inject
    CustomerService customerService;

    @GET
    @RolesAllowed(Roles.CUSTOMER_READ)
    @Operation(summary = "List customers")
    @APIResponse(responseCode = "200", description = "OK")
    public List<Customer> listCustomers() {
        return customerService.listCustomers();
    }

    @GET
    @Path("{customerId}")
    @RolesAllowed(Roles.CUSTOMER_READ)
    @Operation(summary = "Get customer by id")
    @APIResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = CustomerDetail.class)))
    @APIResponse(responseCode = "404", description = "Not found")
    public CustomerDetail getCustomer(@PathParam("customerId") Long customerId) {
        return customerService.findCustomerDetail(customerId).orElseThrow(NotFoundException::new);
    }

    @POST
    @RolesAllowed(Roles.CUSTOMER_WRITE)
    @Operation(summary = "Create customer")
    @APIResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(schema = @Schema(implementation = CustomerDetail.class)))
    @APIResponse(responseCode = "400", description = "Invalid request")
    public Response createCustomer(@Valid CreateCustomerRequest request) {
        validateCreate(request);
        Customer created;
        if (request.customerType() == CustomerType.INDIVIDUAL) {
            created = customerService.create(request.individual());
        } else {
            created = customerService.create(request.business());
        }
        CustomerDetail body =
                customerService.findCustomerDetail(created.customerId()).orElseThrow(NotFoundException::new);
        URI location = URI.create("/api/customers/" + created.customerId());
        return Response.created(location).entity(body).build();
    }

    @PUT
    @Path("{customerId}")
    @RolesAllowed(Roles.CUSTOMER_WRITE)
    @Operation(summary = "Update customer")
    @APIResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = CustomerDetail.class)))
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "404", description = "Not found")
    public CustomerDetail updateCustomer(
            @PathParam("customerId") Long customerId, @Valid UpdateCustomerRequest request) {
        validateUpdate(request);
        var customer = customerService.findCustomer(customerId).orElseThrow(NotFoundException::new);
        if (customer.customerType() == CustomerType.INDIVIDUAL) {
            if (request.individual() == null) {
                throw new BadRequestException("individual payload required for INDIVIDUAL customer");
            }
            CustomerIndividual ind = request.individual();
            customerService.update(
                    customerId,
                    new CustomerIndividual(
                            customerId,
                            ind.firstName(),
                            ind.middleName(),
                            ind.lastName(),
                            ind.suffix()));
        } else {
            if (request.business() == null) {
                throw new BadRequestException("business payload required for BUSINESS customer");
            }
            customerService.update(customerId, new CustomerBusiness(customerId, request.business().legalName()));
        }
        return customerService.findCustomerDetail(customerId).orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("{customerId}")
    @RolesAllowed(Roles.CUSTOMER_WRITE)
    @Operation(summary = "Delete customer")
    @APIResponse(responseCode = "204", description = "Deleted or no content")
    @APIResponse(responseCode = "404", description = "Not found")
    public Response deleteCustomer(@PathParam("customerId") Long customerId) {
        if (!customerService.delete(customerId)) {
            throw new NotFoundException();
        }
        return Response.noContent().build();
    }

    private static void validateCreate(CreateCustomerRequest req) {
        if (req.customerType() == CustomerType.INDIVIDUAL) {
            if (req.individual() == null) {
                throw new BadRequestException("individual is required when customerType is INDIVIDUAL");
            }
            if (req.business() != null) {
                throw new BadRequestException("business must be absent when customerType is INDIVIDUAL");
            }
        } else if (req.customerType() == CustomerType.BUSINESS) {
            if (req.business() == null) {
                throw new BadRequestException("business is required when customerType is BUSINESS");
            }
            if (req.individual() != null) {
                throw new BadRequestException("individual must be absent when customerType is BUSINESS");
            }
        } else {
            throw new BadRequestException("Unsupported customerType");
        }
    }

    private static void validateUpdate(UpdateCustomerRequest req) {
        if (req.individual() != null && req.business() != null) {
            throw new BadRequestException("Send exactly one of individual or business");
        }
        if (req.individual() == null && req.business() == null) {
            throw new BadRequestException("Send individual or business");
        }
    }
}
