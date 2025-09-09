package com.mochafund.identityservice.workspace.membership.controller;

import com.mochafund.identityservice.workspace.membership.service.IMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/memberships")
public class MembershipController {

    private final IMembershipService membershipService;
}
