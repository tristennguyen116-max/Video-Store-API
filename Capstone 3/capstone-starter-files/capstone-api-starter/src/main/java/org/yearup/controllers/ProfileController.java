package org.yearup.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.models.Profile;
import org.yearup.models.User;
import org.yearup.service.ProfileService;
import org.yearup.service.UserService;

import java.security.Principal;

// Added new class for user profile
@RestController
@RequestMapping("profile")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class ProfileController
{
    private final ProfileService profileService;
    private final UserService userService;

    public ProfileController(ProfileService profileService, UserService userService)
    {
        this.profileService = profileService;
        this.userService = userService;
    }

    private int getUserId(Principal principal)
    {
        User user = userService.getByUserName(principal.getName());
        return user.getId();
    }

    @GetMapping
    public Profile getProfile(Principal principal)
    {
        return profileService.getByUserId(getUserId(principal));
    }

    @PutMapping
    public Profile updateProfile(@RequestBody Profile profile, Principal principal)
    {
        return profileService.update(getUserId(principal), profile);


    }
}